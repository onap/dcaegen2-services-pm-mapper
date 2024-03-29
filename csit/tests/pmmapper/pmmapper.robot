*** Settings ***
Documentation     Testing PM Mapper functionality
Library           Collections
Library           OperatingSystem
Library           RequestsLibrary
Library           Process
Library           String

Test Setup        Create Session  mapper_session  ${PMMAPPER_BASE_URL}
Test Teardown     CleanSessionsAndLogs

*** Variables ***
${CLI_EXEC_CLI_CONFIG}                   { head -n 100 | tail -50;} < /tmp/pmmapper.log
${CLI_EXEC_CLI_SUBS}                     curl -k http://${DR_PROV_IP}:8080/internal/prov
${PMMAPPER_BASE_URL}                     http://${PMMAPPER_IP}:8081
${DELIVERY_ENDPOINT}                     /delivery
${HEALTHCHECK_ENDPOINT}                  /healthcheck
${RECONFIGURE_ENDPOINT}                  /reconfigure
${ASSETS_PATH}                           %{WORKSPACE}/tests/pmmapper/assets
${NO_MANAGED_ELEMENT_PATH}               ${ASSETS_PATH}/A_no_managed_element.xml
${NO_MEASDATA_PATH}                      ${ASSETS_PATH}/A_no_measdata.xml
${VALID_METADATA_PATH}                   ${ASSETS_PATH}/valid_metadata.json
${NR_VALID_METADATA_PATH}                ${ASSETS_PATH}/new_radio/valid_metadata.json
${DIFF_VENDOR_METADATA}                  ${ASSETS_PATH}/diff_vendor_metadata.json
${NON_XML_FILE}                          ${ASSETS_PATH}/diff_vendor_metadata.json
${CLI_EXEC_CLI_PM_LOG}                   docker exec pmmapper /bin/sh -c "cat /var/log/ONAP/dcaegen2/services/pm-mapper/pm-mapper_output.log"
${CLI_EXEC_CLI_PM_LOG_CLEAR}             docker exec pmmapper /bin/sh -c "echo -n "" > /var/log/ONAP/dcaegen2/services/pm-mapper/pm-mapper_output.log"
${PUBLISH_NODE_URL}                      http://${DR_NODE_IP}:8080/publish/1
${TYPE-A_PM_DATA_FILE_PATH}              ${ASSETS_PATH}/A20181002.0000-1000-0015-1000_5G.xml
${TYPE-C_PM_DATA_FILE_PATH}              ${ASSETS_PATH}/C20190328.0000-0015.xml
${NR-TYPE-A_PM_DATA_FILE_PATH}           ${ASSETS_PATH}/new_radio/A20181004.0000-1000-0015-1000_5G.xml
${NR-TYPE-C_PM_DATA_FILE_PATH}           ${ASSETS_PATH}/new_radio/C20190329.0000-0015.xml
${NR-TYPE-PM_DATA_FILE_PATH}             ${ASSETS_PATH}/new_radio/PM202007171301+020024C202007171207+0200-1215+0200_45678.xml
${CLI_EXEC_VENDOR_FILTER}                cp ${ASSETS_PATH}/vendor_filter_config.yaml /var/tmp/config.yaml
${CLI_EXEC_PM_FILTER}                    cp ${ASSETS_PATH}/pm_filter_config.yaml /var/tmp/config.yaml
${CLI_EXEC_PM_FILTER_regex}              cp ${ASSETS_PATH}/pm_filter_regex_config.yaml /var/tmp/config.yaml

${CLI_MESSAGE_ROUTER_TOPIC}              curl http://${DMAAP_MR_IP}:3904/events/PM_MAPPER/CG1/C1?timeout=1000 > /tmp/mr.log
${CLI_MR_LOG}                            cat /tmp/mr.log


*** Test Cases ***
Verify PM Mapper Receive Configuraton From Config Binding Service
    [Tags]                          PM_MAPPER_01
    [Documentation]                 Verify 3gpp pm mapper successfully receive config data from CBS
    CheckLog                        ${CLI_EXEC_CLI_CONFIG}           PM-mapper configuration processed successful

Verify Health Check returns 200 when a REST GET request to healthcheck url
    [Tags]                          PM_MAPPER_02
    [Documentation]                 Verify Health Check returns 200 when a REST GET request to healthcheck url
    [Timeout]                       1 minute
    ${resp}=                        Get Request                      mapper_session  ${HEALTHCHECK_ENDPOINT}
    VerifyResponse                  ${resp.status_code}              200

Verify 3GPP PM Mapper responds appropriately when no metadata is provided
    [Tags]                          PM_MAPPER_03
    [Documentation]                 Verify 3GPP PM Mapper responds 400 with the message "Missing Metadata." when no metadata is provided
    [Timeout]                       1 minute
    ${headers}=                     Create Dictionary               X-ONAP-RequestID=3  Content-Type=application/xml
    ${resp}=                        Put Request                     mapper_session  ${DELIVERY_ENDPOINT}/filename    data='${EMPTY}'    headers=${headers}
    VerifyResponse                  ${resp.status_code}             400
    VerifyResponse                  ${resp.content}                 Missing Metadata.
    CheckLog                        ${CLI_EXEC_CLI_PM_LOG}          RequestID=3

Verify 3GPP PM Mapper responds appropriately when invalid metadata is provided
    [Tags]                          PM_MAPPER_04
    [Documentation]                 Verify 3GPP PM Mapper responds 400 with the message "Malformed Metadata." when invalid metadata is provided
    [Timeout]                       1 minute
    ${headers}=                     Create Dictionary               X-ONAP-RequestID=4  X-DMAAP-DR-META='not metadata'  Content-Type=application/xml
    ${resp}=                        Put Request                     mapper_session  ${DELIVERY_ENDPOINT}/filename  data='${EMPTY}'  headers=${headers}
    VerifyResponse                  ${resp.status_code}             400
    VerifyResponse                  ${resp.content}                 Malformed Metadata.
    CheckLog                        ${CLI_EXEC_CLI_PM_LOG}          RequestID=4

Verify that PM Mapper logs successful when a file that contains no measdata is provided
    [Tags]                          PM_MAPPER_05
    [Documentation]                 Verify that PM Mapper logs successful when a file that contains no measdata is provided
    [Timeout]                       1 minute
    SendToDatarouter                ${NO_MEASDATA_PATH}              ${VALID_METADATA_PATH}            X-ONAP-RequestID=5
    CheckLog                        ${CLI_EXEC_CLI_PM_LOG}           MeasData is empty
    CheckLog                        ${CLI_EXEC_CLI_PM_LOG}           RequestID=5

Verify that PM Mapper throws Event failed validation against schema error when no managed element content is provided
    [Tags]                          PM_MAPPER_06
    [Documentation]                 Verify 3gpp pm mapper responds with an error when no managed element content is provided
    [Timeout]                       1 minute
    SendToDatarouter                ${NO_MANAGED_ELEMENT_PATH}       ${VALID_METADATA_PATH}             X-ONAP-RequestID=6
    CheckLog                        ${CLI_EXEC_CLI_PM_LOG}           XML validation failed
    CheckLog                        ${CLI_EXEC_CLI_PM_LOG}           RequestID=6

Verify that PM Mapper maps Type-C xml file and publish 3gpp perf VES evnets to message router
    [Tags]                          PM_MAPPER_07
    [Documentation]                 Verify that PM Mapper maps Type-C xml file and publish 3gpp perf VES evnets to message router.
    [Timeout]                       1 minute
    SendToDatarouter                ${TYPE-C_PM_DATA_FILE_PATH}      ${VALID_METADATA_PATH}           X-ONAP-RequestID=7
    CheckLog                        ${CLI_EXEC_CLI_PM_LOG}           Successfully published VES events to messagerouter

Verify 3GPP PM Mapper maps Type-A file based on counter filtering and publish 3gpp perf VES evnets to message router
    [Tags]                          PM_MAPPER_08
    [Documentation]                 Verify 3GPP PM Mapper maps Type-A file and publish 3gpp perf VES evnets to message router.
    [Timeout]                       1 minute
    ${cli_cmd_output}=              Run Process                      ${CLI_EXEC_PM_FILTER}             shell=yes
    ${resp}=                        Get Request                      mapper_session                    ${RECONFIGURE_ENDPOINT}
    Sleep                           5s
    SendToDatarouter                ${TYPE-A_PM_DATA_FILE_PATH}      ${VALID_METADATA_PATH}            X-ONAP-RequestID=8
    CheckLog                        ${CLI_EXEC_CLI_PM_LOG}           Successfully published VES events to messagerouter

Verify that PM Mapper correctly identifies a file that should not be mapped based on metadata filtering.
    [Tags]                          PM_MAPPER_09
    [Documentation]                 Verify that PM Mapper correctly identifies a file that should not be mapped based on metadata filtering.
    [Timeout]                       1 minute
    ${cli_cmd_output}=              Run Process                      ${CLI_EXEC_VENDOR_FILTER}         shell=yes
    Should Be Equal As Strings      ${cli_cmd_output.rc}             0
    ${resp}=                        Get Request                      mapper_session                    ${RECONFIGURE_ENDPOINT}
    Sleep                           5s
    SendToDatarouter                ${TYPE-A_PM_DATA_FILE_PATH}      ${DIFF_VENDOR_METADATA}           X-ONAP-RequestID=9
    CheckLog                        ${CLI_EXEC_CLI_PM_LOG}           RequestID=9
    CheckLog                        ${CLI_EXEC_CLI_PM_LOG}           Metadata does not match any filters

Verify that PM Mapper correctly identifies a non-xml file.
    [Tags]                          PM_MAPPER_10
    [Documentation]                 Verify that PM Mapper correctly identifies a non-xml file.
    [Timeout]                       1 minute
    SendToDatarouter                ${NON_XML_FILE}                  ${VALID_METADATA_PATH}             X-ONAP-RequestID=10
    CheckLog                        ${CLI_EXEC_CLI_PM_LOG}           PM measurement file must have an extension of .xml
    CheckLog                        ${CLI_EXEC_CLI_PM_LOG}           RequestID=10

Verify that PM Mapper correctly maps an NR Type-A file based on counter filtering and publish 3gpp perf VES events to message router.
    [Tags]                          PM_MAPPER_11
    [Documentation]                 Verify 3GPP PM Mapper maps an NR Type-A file and publish 3gpp perf VES evnets to message router.
    [Timeout]                       1 minute
    ${cli_cmd_output}=              Run Process                      ${CLI_EXEC_PM_FILTER}             shell=yes
    ${resp}=                        Get Request                      mapper_session                    ${RECONFIGURE_ENDPOINT}
    Sleep                           5s
    SendToDatarouter                ${NR-TYPE-A_PM_DATA_FILE_PATH}   ${NR_VALID_METADATA_PATH}            X-ONAP-RequestID=11
    CheckLog                        ${CLI_EXEC_CLI_PM_LOG}           Successfully published VES events to messagerouter

Verify that PM Mapper correctly maps an NR Type-C file based on counter filtering and publish 3gpp perf VES events to message router.
    [Tags]                          PM_MAPPER_12
    [Documentation]                 Verify that PM Mapper maps an NR Type-C xml file and publish 3gpp perf VES evnets to message router.
    [Timeout]                       1 minute
    SendToDatarouter                ${NR-TYPE-C_PM_DATA_FILE_PATH}    ${NR_VALID_METADATA_PATH}           X-ONAP-RequestID=12
    CheckLog                        ${CLI_EXEC_CLI_PM_LOG}            Successfully published VES events to messagerouter

Verify 3GPP PM Mapper maps Type-A file based on counter filtering with regexp
    [Tags]                          PM_MAPPER_13
    [Documentation]                 Verify 3GPP PM Mapper maps Type-A file based on counter filtering with wildcards/regexp and publish 3gpp perf VES evnets to message router.
    [Timeout]                       1 minute
    ${cli_cmd_output}=              Run Process                      ${CLI_EXEC_PM_FILTER_regex}             shell=yes
    ${resp}=                        Get Request                      mapper_session                    ${RECONFIGURE_ENDPOINT}
    Sleep                           5s
    SendToDatarouter                ${TYPE-A_PM_DATA_FILE_PATH}      ${VALID_METADATA_PATH}            X-ONAP-RequestID=13
    CheckLog                        ${CLI_EXEC_CLI_PM_LOG}           Successfully published VES events to messagerouter

Verify that password receive from CBS are successfully encrypted
    [Tags]                          PM_MAPPER_14
    [Documentation]                 Verify that password receive from CBS are successfully encrypted.
    CheckLog                        ${CLI_EXEC_CLI_CONFIG}           aafPassword= *****
    CheckLog                        ${CLI_EXEC_CLI_CONFIG}           password= *****

Verify that PM Mapper correctly maps an NR Type-PM file based on counter filtering and publish 3gpp perf VES events to message router.
    [Tags]                          PM_MAPPER_15
    [Documentation]                 Verify that PM Mapper maps an NR Type-PM xml file and publish 3gpp perf VES evnets to message router.
    [Timeout]                       1 minute
    SendToDatarouter                ${NR-TYPE-PM_DATA_FILE_PATH}      ${NR_VALID_METADATA_PATH}           X-ONAP-RequestID=15
    CheckLog                        ${CLI_EXEC_CLI_PM_LOG}            RequestID=15
    CheckLog                        ${CLI_EXEC_CLI_PM_LOG}            Successfully published VES events to messagerouter

*** Keywords ***

SendToDatarouter
    [Arguments]                     ${filepath}                      ${metadatapath}            ${request_id}
    ${pmdata}=                      Get File                         ${filepath}
    ${metatdata}                    Get File                         ${metadatapath}
    ${filename}                     Fetch From Right                 ${filepath}                /
    ${resp}=                        PutCall                          ${PUBLISH_NODE_URL}/${filename}        ${request_id}    ${pmdata}    ${metatdata.replace("\n","")}    pmmapper
    VerifyResponse                  ${resp.status_code}              204
    Sleep                           10s

PutCall
    [Arguments]                     ${url}                           ${request_id}              ${data}            ${meta}          ${user}
    ${headers}=                     Create Dictionary                X-ONAP-RequestID=${request_id}                X-DMAAP-DR-META=${meta}    Content-Type=application/octet-stream     X-DMAAP-DR-ON-BEHALF-OF=${user}    Authorization=Basic cG1tYXBwZXI6cG1tYXBwZXI=
    ${resp}=                        Evaluate                         requests.put('${url}', data="""${data}""", headers=${headers}, verify=False, allow_redirects=False)    requests
    [Return]                        ${resp}

CheckLog
    [Arguments]                     ${cli_exec_log_Path}             ${string_to_check_in_log}
    ${cli_cmd_output}=              Run Process                      ${cli_exec_log_Path}                     shell=yes
    Log                             ${cli_cmd_output.stdout}
    Should Be Equal As Strings      ${cli_cmd_output.rc}             0
    Should Contain                  ${cli_cmd_output.stdout}         ${string_to_check_in_log}

VerifyResponse
    [Arguments]                     ${actual_response_value}         ${expected_response_value}
    Should Be Equal As Strings      ${actual_response_value}         ${expected_response_value}

ClearLogs
    Run Process                     ${CLI_EXEC_CLI_PM_LOG_CLEAR}                     shell=yes

CleanSessionsAndLogs
    Delete All Sessions
    ClearLogs
