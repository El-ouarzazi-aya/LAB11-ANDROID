<?php
class Position {
    private $id;
    private $latitude;
    private $longitude;
    private $datePosition;
    private $imei;

    public function __construct($id, $latitude, $longitude, $datePosition, $imei) {
        $this->id = $id;
        $this->latitude = $latitude;
        $this->longitude = $longitude;
        $this->datePosition = $datePosition;
        $this->imei = $imei;
    }

    public function getId() { return $this->id; }
    public function getLatitude() { return $this->latitude; }
    public function getLongitude() { return $this->longitude; }
    public function getDatePosition() { return $this->datePosition; }
    public function getImei() { return $this->imei; }
}
?>