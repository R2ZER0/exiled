/* World Manager */

/* TODO make a Tile class */

var Tile = (function() {
    
    function Tile(tile) {
        if(tile) {
            this.copyFrom(tile);
        } else {
            this.x = NaN;
            this.y = NaN;
            this.height = NaN;
            this.kind = "";
    }
    
    Tile.prototype.clone = function() {
        return new Tile(this);
    };
    
    Tile.prototype.copyFrom(tile) {
        this.x = tile.x;
        this.y = tile.y;
        this.height = tile.height;
        this.kind = tile.kind;
    }
    
    return Tile;
});

/* We maintain a mapping of location -> tile */
var World = (function() {
    
    /* Takes the URL of a websocket from which to get the world */
    function World(url, ready) {
        var tiles = new RTree();
        var onupdate = function() {};
        
        var self = this;
        
        var ws = $.websocket(url, {
          open: function() { ready(); },
          close: function(e) {
            console.log('Socket is closed. Reconnect will be attempted in 1 second.', e.reason);
            setTimeout(function() {
              connect();
            }, 1000)
          },
          events: { 
            gottile: function(e) {
              self._setTile(new Tile(e.data.tile));
              var fn = self._onupdate;
              fn();
            },
            gottiles: function(e) {
              self._setTiles(new Tile(e.data.tiles));
              var fn = self._onupdate;
              fn();
            }
          }
        });
        
        $(window).on('beforeunload', function(){
            ws.close();
        });
        
        this._tiles = tiles;
        this._onupdate = onupdate;
        this._ws = ws;
    }
    
    /* Find all (known) tiles in the given area */
    World.prototype.search = function(rect) {
        return this._tiles.search(rect);
    };
    
    /* Set the callback to be called when we have gotten new tiles */
    World.prototype.onUpdate = function(cb) {
        this._onupdate = cb;
    };
    
    /* We need this area, fetch it if neccessary */
    World.prototype.needArea = function(rec) {
        var needed = new Array();
      
        for(var y = rec.y; y < rec.y+rec.h; y++) {
          for(var x = rec.x; x < rec.x+rec.w; x++) {
            if(!this.getTile(x, y)) {
              needed.push({x:x, y:y});
            }
          }
        }
        
        this.fetch(needed);
    };
    
    /* Requests loading a given list of coords */
    World.prototype.fetch = function(tiles) {
        this._ws.send('gettiles', { tiles: tiles });
    };
    
    /* Requests loading the given area */
    World.prototype.fetchArea = function(rec) {
        this._ws.send('getarea', { area: rec });
    };
    
    /* Get a single tile at the given position */
    World.prototype.getTile = function(x, y) {
        return this._tiles.search(
            { x:x+0.5, y:y+0.5, w:0, h:0 } // search at the centre of the tile
        )[0];
    };
    
    World.prototype._setTile = function(tile) {
        var gottile = this.getTile(tile.x, tile.y);
        if(gottile) {
            gottile.copyFrom(tile);
        } else {
            this._tiles.insert(
                { x:tile.x, y:tile.y, w:1, h:1 },
                tile
            );
        }
    };
    
    World.prototype._setTiles = function(tiles) {
        tiles.forEach(function(tile) {
            this._setTile(tile);
        }, this);
    };
    
    /* TODO: Ability to prune, so we remove tiles we don't need anymore */
  return World;
})();