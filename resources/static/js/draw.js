/* Draw! */

var SCALE     = 1;
var TILE_SIZE = 16;

var canvas = document.getElementById('canvas');
var ctx = canvas.getContext('2d');

var waterStyle = "rgb(0,0,255)";
var grassStyle = "rgb(0,255,0)";
var peakStyle  = "rgb(128,128,128)";

function height2style(height) {
  var norm = Math.floor(255 * height);
//   if(norm < 75) { return waterStyle; }
//   else if(norm < 200) { return grassStyle; }
//   else { return peakStyle; }
  return "rgb(" + norm + "," + norm + "," + norm + ")";
}

function drawTileAt(x, y, tile) {
  ctx.fillStyle = height2style(tile.height);
  ctx.fillRect(
      x,
      y,
      TILE_SIZE ,
      TILE_SIZE
  );
}

function drawWorld(world, view) {
    var canvas = document.getElementById("canvas");
    // Clear the canvas
    ctx.fillStyle = "#000";
    ctx.fillRect(0, 0, canvas.width, canvas.height);
  
    var tiles = world.search(view);
    tiles.forEach(function (tile) {
        drawTileAt(
          TILE_SIZE*(tile.x - view.x),
          TILE_SIZE*(tile.y - view.y),
          tile);
    });
}