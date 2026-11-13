package pe.greenminds.ecomind.community.presentation.ui.map

import android.text.TextUtils

internal fun EventMapHtml(errorMessage: String): String = """
<!DOCTYPE html>
<html>
<head>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <link rel="stylesheet"
    href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"
    integrity="sha256-p4NxAoJBhIIN+hmNHrzRCf9tD/miZyoHS5obTRR9BMY="
    crossorigin="">
  <style>
    html, body {
      margin: 0;
      padding: 0;
      width: 100%;
      height: 100%;
      overflow: hidden;
    }
    
    #map {
      position: fixed;
      inset: 0;
      width: 100vw;
      height: 100vh;
    }
    #error {
      position:absolute;
      z-index:1000;
      top:4px;
      left:42px;
      right:4px;
      padding:5px;
      background:white;
      color:#333;
      font:11px sans-serif;
    }
    .leaflet-control-attribution {
      font-size:9px;
    }
  </style>
</head>
<body>
  <div id="map"></div>
  <div id="error">${TextUtils.htmlEncode(errorMessage)}</div>
  <script
    src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"
    integrity="sha256-20nQCchB9co0qIjJZRGuk2/Z9VM+kNiyxNV1lvTlZBo="
    crossorigin=""></script>
  <script>
    if (window.L) {
      const error = document.getElementById('error');
      error.style.display = 'none';

      const map = L.map('map').setView([-12.0464, -77.0428], 13);
      requestAnimationFrame(() => {
        map.invalidateSize();
      });
    
      window.addEventListener('resize', () => {
         map.invalidateSize();
      });
      const tiles = L.tileLayer(
        'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
        {
          maxZoom: 19,
          attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        }
      ).addTo(map);

      tiles.on('tileerror', () => {
        error.style.display = 'block';
      });

      tiles.on('tileload', () => {
        error.style.display = 'none';
      });

      let marker = null;

      function place(lat, lng) {
        const point = L.latLng(lat, lng);

        if (marker) {
          marker.setLatLng(point);
        } else {
          marker = L.marker(point, { draggable:true }).addTo(map);

          marker.on('dragend', () => {
            const p = marker.getLatLng().wrap();
            marker.setLatLng(p);
            EventLocation.selected(p.lat, p.lng);
          });
        }
      }

      map.on('click', event => {
        const p = event.latlng.wrap();
        place(p.lat, p.lng);
        EventLocation.selected(p.lat, p.lng);
      });

      window.setEventPoint = function(lat, lng) {
        if (lat === null || lng === null) {
          if (marker) {
            map.removeLayer(marker);
            marker = null;
          }
          return;
        }

        place(lat, lng);
        map.panTo([lat, lng], { animate:false });
      };
    }
  </script>
</body>
</html>
""".trimIndent()