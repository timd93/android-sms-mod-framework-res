<?php
$allowedExts = array("apk");
$extension = end(explode(".", $_FILES["file"]["name"]));
$filename = $_FILES["file"]["name"];
$basename = reset(explode(".apk", $_FILES["file"]["name"]));
if (file_exists("upload/" . $_FILES["file"]["name"]))
	{
		unlink("upload/" . $filename);
	}
if (in_array($extension, $allowedExts))
  {
  if ($_FILES["file"]["error"] > 0)
    {
    echo "Return Code: " . $_FILES["file"]["error"] . "<br />";
    }
  else
    {
    echo "Upload: " . $_FILES["file"]["name"] . "<br />";
    echo "Type: " . $_FILES["file"]["type"] . "<br />";
    echo "Size: " . ($_FILES["file"]["size"] / 1024) . " Kb<br />";
    echo "Temp file: " . $_FILES["file"]["tmp_name"] . "<br />";

    if (file_exists("upload/" . $filename))
      {
      echo $_FILES["file"]["name"] . " already exists. ";
      }
    else
      {
      $md5 = md5_file($_FILES["file"]["tmp_name"]);
      move_uploaded_file($_FILES["file"]["tmp_name"],
      "upload/" . $basename . "_" . $md5  . "." . $extension);
      echo "Stored in: " . "upload/" . $basename . "_" . $md5 . "." .$extension;
      }
    }
  }
else
  {
  echo "Invalid file";
  }
?>
<?php

?>