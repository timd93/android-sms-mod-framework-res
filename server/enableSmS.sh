#!/bin/bash
file=$1
dir=$(echo $file | cut -d_ -f1)
fileAndDirAdded=("upload/"$1)
uploadedMD5=$2
uploadedName=("upload/""$dir""_""$uploadedMD5"".apk")
decompileDir=("$dir""_""$uploadedMD5")
newFile=("$decompileDir""_new.apk")
#originMD5=`md5sum $fileAndDirAdded| cut -d' ' -f1`
#originFile=("$dir""_""$originMD5"".apk")
#Decompile
echo ------------------
echo STARTING DECOMPILE
echo ------------------
./apktool d -s $uploadedName
echo ------------------
echo STARTING EDITING
echo ------------------
#Enable sms
findString="<bool name=\"config_sms_capable\">false<\/bool>"
replaceString="<bool name=\"config_sms_capable\">true<\/bool>"
cd $decompileDir 
cd res/values
sed -i 's,'"$findString"','"$replaceString"',' "bools.xml"
echo ------------------
echo STARTING RECOMPILE
echo ------------------
#Recompile
cd -
cd ..
./apktool b $decompileDir "upload/$newFile"
echo "FINISHED"
