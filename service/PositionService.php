<?php
require_once __DIR__ . '/../connexion/Connexion.php';
require_once __DIR__ . '/../classe/Position.php';

class PositionService {
    private $pdo;

    public function __construct() {
        $connexion = new Connexion();
        $this->pdo = $connexion->getPdo();
    }

    public function create(Position $position) {
        $sql = "INSERT INTO position (latitude, longitude, date_position, imei)
                VALUES (:latitude, :longitude, :date_position, :imei)";

        $stmt = $this->pdo->prepare($sql);
        $stmt->execute([
            ':latitude'      => $position->getLatitude(),
            ':longitude'     => $position->getLongitude(),
            ':date_position' => $position->getDatePosition(),
            ':imei'          => $position->getImei()
        ]);
    }

    public function getAll() {
        $stmt = $this->pdo->query(
            "SELECT * FROM position ORDER BY date_position DESC"
        );
        return $stmt->fetchAll(PDO::FETCH_ASSOC);
    }
}
?>