<?php
/**
 * Client sends the COTD message to request information on the charity of the day
 * If this is the first message concerning the COTD, we need to select a COTD.
 * Then, send the COTD information back to the client.
 */   
class CharityOfTheDayHandler extends MessageHandler
{
   function getId()
   {
      return 1;
   }

   function process($msg)
   {
      $db = new DbConn();
      $db->open();
      
      // Is there a COTD for today yet?
      if (!$this->doesCotdExistForToday($db))
      {
         // Nope; create it
         if (!$this->isCharityDatabaseEmpty($db))
         {
            // Get a random charity from the charity db
            // Thanks to http://akinas.com/pages/en/blog/mysql_random_row/ for this snippet
            $offset = $db->queryAsObject("SELECT FLOOR(RAND() * COUNT(*)) AS `offset` FROM `charities` ")->offset;
            $charity = $db->queryAsObject("SELECT * FROM `charities` LIMIT $offset, 1 ");
            
            // Now, create a COTD record corresponding to this charity and today's date
            $db->queryAsObject("INSERT INTO cotds(charity, date) VALUES(" . $charity->id . ",\"" . date("Y-m-d") . "\")");
         }
         else
         {
            // Bizarre case where there are no charities in the database
            throw new Exception("The charities database is empty");
         }
      }
      else
      {
         // COTD entry exists; now get info on the charity
         $cotd = $db->queryAsObject("SELECT charity FROM cotds WHERE date = \"" . date("Y-m-d") . "\"");
         $charity = $db->queryAsObject("SELECT * FROM charities WHERE id = " . $cotd->charity);
         if ($charity == null)
         {
            throw new Exception("Charity of id " . $cotd->charity . " does not exist");
         }
      }

      // Format charity info into a message to send back
      $response = (object)array("responseId"=>1,"data"=>$charity);
      return json_encode($response);
   }
   
   private function doesCotdExistForToday($db)
   {
      $raw = $db->queryAsObject("SELECT COUNT(*) AS 'count' FROM cotds WHERE date = \"" . date("Y-m-d") . "\"");
      return ($raw->count > 0);
   }

   private function isCharityDatabaseEmpty($db)
   {
      $raw = $db->queryAsObject("SELECT COUNT(*) AS 'count' FROM charities");
      return ($raw->count == 0);
   }
};
?>