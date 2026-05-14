<?php
header("Content-Type: text/plain; charset=utf-8");
header("Access-Control-Allow-Origin: *");

require_once __DIR__ . '/service/PositionService.php';

if ($_SERVER["REQUEST_METHOD"] !== "POST") {
    http_response_code(405);
    echo "Méthode non autorisée";
    exit;
}

$latitude     = $_POST["latitude"]     ?? null;
$longitude    = $_POST["longitude"]    ?? null;
$datePosition = $_POST["date_position"] ?? date("Y-m-d H:i:s");
$imei         = $_POST["imei"]         ?? "inconnu";

if ($latitude === null || $longitude === null) {
    http_response_code(400);
    echo "Paramètres manquants : latitude et longitude requis";
    exit;
}

$position = new Position(null, $latitude, $longitude, $datePosition, $imei);
$service  = new PositionService();
$service->create($position);

echo "Position enregistrée — " . $datePosition;
?>