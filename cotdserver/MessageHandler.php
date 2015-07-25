<?php
// A MessageHandler represents one message type that is accepted by the server.
// To create a new message, create a subclass of MessageHandler in the /messages folder
// Make sure to include the file at the top of cotd.php
abstract class MessageHandler
{
   // This function should return an int corresponding to the message id
   abstract function getId();
   
   // This function should accept one parameter which contains the message data as an object from json_decode
   // This function should return one parameter which contains the response as an object from json_encode
   abstract function process($msg);
};
?>