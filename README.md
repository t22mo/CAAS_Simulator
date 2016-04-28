# CAAS_Simulator

###Requirements
    JRE version 1.8 (or higher)
    "res" folder and "input.txt" must be located in same foler with executable.

###input.txt JSON definition
    {
      "nodelist" : 
      [
        {
          "id":Integer,           // Identifier 
          "x":Integer,            // X coordinate
          "y":Integer,            // Y coordinate
          "view_x":Integer,       // View direction vector X
          "view_y":Integer,       // View direction vector Y
          "view_angle":Integer,   // View angle in degrees (0~360)
          "view_distance":Integer,// View distance
          "port":Integer          // Port
        },
        {
        ...
        }
      
      ]
    }
    

###Instructions
    Execution: Open "CAAS-simulator.jar" OR java -jar CAAS-simulator.jar in terminal




