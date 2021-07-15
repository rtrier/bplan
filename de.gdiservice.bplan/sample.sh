#!/bin/bash
SCRIPTPATH="$( cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"
APP_PATH=`dirname ${SCRIPTPATH}`
echo "APP_PATH=${APP_PATH}"

docker run -d --name bplanImporterTest -v ${APP_PATH}/conf_dev:/app/conf bplan_importer \
 dburl=dbuser@19.10.10.0:5433/kvwmapsp \
 pgpass=/app/conf/pgpass \
 cronExpr="0 45 0 * * ?" \
 kvwmap_url=http://bauleitplaene-mv.de:8085/kvwmap_dev/index.php \
 kvwmap_username=secret \
 kvwmap_password="secret" \
 emailUser=emailuser \
 emailPwd="secret" \
 runNow=true
