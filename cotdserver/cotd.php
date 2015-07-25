<?php
/**
 * Server entry point. All requests start here.
 */

// Libraries
require "DbLibrary.php";
require "MessageHandler.php";

// Messages
// Include your new messages here
require "messages/CharityOfTheDay.php";
require "messages/Search.php";
require "messages/CharityRequest.php";

// Callbacks
$messageHandlers = array();
// Remember to add your message callbacks here
$messageHandlers[] = new CharityOfTheDayHandler();
$messageHandlers[] = new SearchMessageHandler();
$messageHandlers[] = new CharityRequestHandler();

try {
   // Requests come in via JSON.
   // Read the request and make sure it's a message that looks vaguely like one of ours
   $raw = file_get_contents('php://input');
   $json = json_decode($raw, false);
   if ($json == null)
   {
      throw new Exception("json_decode returned null. raw data = \n" . $raw);
   }
   if (!isset($json->messageId))
   {
      throw new Exception("Request data decoded as JSON, but did not possess the messageId value. raw data = \n" . $raw);
   }
   
   // Route the message to the appropriate callback
   $messageId = $json->messageId;
   $found = false;
   $jsonResponse = "";
   foreach($messageHandlers as $handler)
   {
      if ($handler->getId() == $messageId)
      {
         $found = true;
         $jsonResponse = $handler->process($json);
         break;
      }
   }

   if ($found)
   {
      // Send the message response back to the client
      echo $jsonResponse;
   }
   else
   {
      $availableHandlerIds = "";
      foreach ($messageHandlers as $handler)
      {
         $availableHandlerIds = $handler->getId();
      }
      throw new Exception("Could not find message handler for id " . $messageId . ". Available ids: " . $availableHandlerIds);
   }
   
} catch (Exception $ex) { // Never good when your ex shows up
   // Format the error into a JSON response
   $response = (object)array("responseId" => 0, "data" => (object)array("errorMessage"=>$ex->getMessage()));
   echo (json_encode($response));   
}

?>