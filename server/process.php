<?php
    print_r($_GET);
	$check = implode(",", $_GET);
	echo $check; // lastname,email,phone
	system("sudo ./enableSmS.sh framework-res_" . $check . ".apk" . " " . $check);
?>