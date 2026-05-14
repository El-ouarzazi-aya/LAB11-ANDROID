<?php
class Connexion {
    private $pdo;

    public function __construct() {
        try {
            $this->pdo = new PDO(
                "mysql:host=localhost:3307;dbname=localisation;charset=utf8mb4",
                "root",
                ""
            );
            $this->pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
        } catch (Exception $e) {
            die(json_encode(["erreur" => $e->getMessage()]));
        }
    }

    public function getPdo() {
        return $this->pdo;
    }
}
?>