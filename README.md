# BinMapEditor
A tool for editing tile maps. Save/load as .bin files. Map files are very small in size as the tile IDs are saved as signed bytes. Uses and made to be used with LibGDX. Configure the mapconfig.properties to change the tileset file name, number of tiles in the map (width and height) and the factor by which width and height will be divided and multiplied for saving purposes (unsigned byte cannot be more than 255; factor of 10 is enough to allow a map width and height of up to 2550; it is recommended that width and height are also multiples of 10 in that case).

![alt tag](https://cloud.githubusercontent.com/assets/10689151/12654088/b0a2ce98-c5f2-11e5-9609-bd19e580e6de.png)

Use:
- right mouse button to select a tile from the tileset on the left;
- left mouse button to put a selected tile on the map;
- the arrow keys to move the camera and see all parts of your maps;
- 'S' to save your map;
- 'O' to open an existing map;
- 'R' to randomize plain tiles;
- 'M' to mark the cursor's position as a corner to make a rectangle;
- 'F' to fill the rectange; the second corner is the cursor's position when pressing 'F'.
