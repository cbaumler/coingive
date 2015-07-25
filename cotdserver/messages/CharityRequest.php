<?php
/**
 * Client sends the charity message to request information on a specific charity
 * Server sends the charity information back to the client.
 */   
class CharityRequestHandler extends MessageHandler
{
   function getId()
   {
      return 3;
   }

   function process($msg)
   {
      $db = new DbConn();
      $db->open();
      
      if (!property_exists($msg, "data")) {
         throw new Exception("Charity request message does not contain data property");
      }
      if (!property_exists($msg->data, "charityId")) {
         throw new Exception("Charity request message data does not contain charity id");
      }
      
      $charityId = filter_var($msg->data->charityId, 
                              FILTER_SANITIZE_NUMBER_INT);

      $charity = $db->queryAsObject("SELECT * FROM charities WHERE id = " . $charityId);
      if ($charity == null)
      {
         throw new Exception("Charity of id " . $charityId . " does not exist");
      }

      // Format charity info into a message to send back
      $response = (object)array("responseId"=>1,"data"=>$charity);
      return json_encode($response);

   }
};
?>