package cn.together.common.core.util;

public class PoiUtil {
    public static int distance(double lng1, double lat1, double lng2, double lat2) {
        double R = 6378137; // 地球半径

        double deltaLng = (lng1 - lng2) * Math.PI / 180.0;

        double latRadian1 = lat1 * Math.PI / 180.0;
        double latRadian2 = lat2 * Math.PI / 180.0;
        double deltaLat = latRadian1 - latRadian2;

        double sinLng = Math.sin(deltaLng / 2.0);
        double sinLat = Math.sin(deltaLat / 2.0);

        return (int) (2 * R * Math.asin(Math.sqrt(sinLat * sinLat + Math.cos(latRadian1) * Math.cos(latRadian2) * sinLng * sinLng)));
    }
}
