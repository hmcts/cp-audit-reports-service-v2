#!/bin/sh

az storage table create --name reportrequests

az storage container create --name downloads # --public-access blob

az storage blob upload --data "<HTML><BODY><P>Example HTML Page</P></BODY></HTML>" --container downloads --name some/folder/example.html --content-type text/html
