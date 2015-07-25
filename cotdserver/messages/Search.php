<?php
/**
 * Client sends the Search message to request all charities that match the search criteria.
 * Server response with one message containing all results
 */   
class SearchMessageHandler extends MessageHandler
{
   function getId()
   {
      return 2;
   }

   function process($msg)
   {
      $db = new DbConn();
      $db->open();
      
      if (!property_exists($msg, "data")) {
         throw new Exception("COTD message does not contain data property");
      }
      if (!property_exists($msg->data, "query")) {
         throw new Exception("COTD message data does not contain search query");
      }
      
      $userQuery = filter_var($msg->data->query, 
                              FILTER_SANITIZE_STRING, 
                              FILTER_FLAG_STRIP_LOW | FILTER_FLAG_STRIP_HIGH);
      
      $queryString = "SELECT id, name FROM charities WHERE (name LIKE '%$userQuery%' OR description LIKE '%$userQuery%')";
      if ($results = $db->query($queryString))
      {
         $response = (object)array("responseId"=>2, "data"=>(object)array("results"=>array()));
         while ($row = $results->fetch_object())
         {
            array_push($response->data->results, $row);
         }
         
         return json_encode($response);      
      }
      else
      {
         throw new Exception("MySQL error while executing search query: $queryString");
      }
   }
};
?>