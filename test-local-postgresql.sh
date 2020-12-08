#!/bin/sh

test -d ../database
if [ $? -ne 0 ]
then
    echo "$0 Database project is not checked out"
    exit 10
fi

curl 127.0.0.1:5432 2>/dev/null >/dev/null
if [ $? -ne 52 ]
then
    echo "$0 Local postgresql is not running"
    exit 20
fi

echo Restoring database...
cd ../database
sh restore-local.sh >/dev/null
if [ $? -ne 0 ]
then
    echo "$0 error restoring local database"
    exit 30
fi

cd ../movies-st
mvn clean package -q
