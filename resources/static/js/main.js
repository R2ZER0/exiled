/* Do stuff! */

var canvas_view = { x:100, y:-50, w:64, h:32 };

var world = new World("ws://localhost:8080/websocket", function() {
    console.log("World ready");
});

$("#button").click(function (){
    var canvas = $("#canvas");
    
    console.log("Doing things!");
    
    // Setup drawing the tiles
    world.onUpdate(function() {
        drawWorld(world, canvas_view);
    });
    
    // Fetch initial tiles
    world.fetchArea(canvas_view);
});

console.log("Created world: " + world);

function copyRectangle(rec) {
    return {
        x: rec.x,
        y: rec.y,
        w: rec.w,
        h: rec.h
    };
}

$(document).keydown(function(e) {
    var changed = true;
    var old_view = copyRectangle(canvas_view);
      
    
    var DELTA = 5;
    
    if(e.key == "Down") {
      canvas_view.y = canvas_view.y + DELTA;
      
    } else if(e.key == "Up") {
      canvas_view.y = canvas_view.y - DELTA;
      
    } else if(e.key == "Left") {
      canvas_view.x = canvas_view.x - DELTA;
      
    } else if(e.key == "Right") {
      canvas_view.x = canvas_view.x + DELTA;
      
    } else {
      changed = false;
    }
    
    /* Calculate the newly exposed area */
    var new_area = copyRectangle(canvas_view);
    if(e.key == "Down") {
      new_area.y = old_view.y + old_view.h;
      new_area.h = DELTA;
      
    } else if(e.key == "Up") {
      new_area.y = old_view.y - DELTA;
      new_area.h = DELTA;
      
    } else if(e.key == "Left") {
      new_area.x = old_view.x - DELTA;
      new_area.w = DELTA;
      
    } else if(e.key == "Right") {
      new_area.x = old_view.x + old_view.w;
      new_area.w = DELTA;      
    } 
    
    if(changed) {
      // Request our unseen tiles
      world.needArea(new_area);
      
      // Draw the world in the new position
      drawWorld(world, canvas_view);
    }
});

/*
function setsize(){
    var w = $(window).innerWidth();
    var h = $(window).innerHeight();
    $("#canvas").width = w;
    $("#canvas").height = h;
}

$(window).bind("resize", setsize);*/