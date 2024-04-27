<?php

class DatabaseLinker {

    public static function getConnexion() {
		
        $host = '192.168.153.10';
        $username = 'epourchon';
        $password = 'Elliot&43';
        $dbname = '202324_courses_epourchon';
    
        $conn = new PDO("mysql:host=$host;dbname=$dbname", $username, $password);
        $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
        return $conn;
    }
}