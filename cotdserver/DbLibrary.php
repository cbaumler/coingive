<?php
/**
 * Connection to the database.
 */
class DbConn {
   private $mysqli;
   private $connected;

   const USERNAME = "mistap_cotd";
   const PASSWORD = "3W!OW{OrHb_6";
   
   function __construct()
   {
      $connected = false;
   }
   
   /**
    * Call this before running any DB transactions
    */
   function open()
   {
      if ($connected) { return; }
      $this->mysqli = new mysqli("localhost", DbConn::USERNAME, DbConn::PASSWORD, "mistap_charityoftheday");
      if ($this->mysqli->connect_errno)
      {
         throw new Exception("Failed to connect to MySQL: (" . $this->mysqli->connect_errno . ") " . $this->mysqli->connect_error);
      }
      $connected = true;
   }
   
   /**
    * Wrapper for mysqli_query that includes error handling
    * @param {String} $queryString The MySQLi query to execute
    * @return The Mysqli result of the query           
    */
   function query($queryString)
   {
      if (!($result = $this->mysqli->query($queryString)))
      {
         throw new Exception("Query ${queryString} failed: (" . $this->mysqli->errno . ") " . $this->mysqli->error);
      }
      return $result;
   }
   
   /**
    * Wrapper for query() for grabbing a single row from the database.
    * @param {String} $queryString See query()    
    * @return The first row of the query as an object. If the query returned nothing, return null
    */           
   function queryAsObject($queryString)
   {
      $result = $this->query($queryString);
      if ($result->num_rows == 1)
      {
         return $result->fetch_object();
      } else {
         return null;
      }
   }
};
?>