# Explore_the_galaxy
[![Github All Releases](https://img.shields.io/github/downloads/MilanTodorovic/Explore_the_galaxy/total.svg)]()

<centered><img src="./logo_pizonko_interlaced.png"></centered>


A console script for Starsector<br><br>

This mod benefits mostly users that have Grand or Adjusted sector mods.<br>
Commands:<br>
1) Explore \[full] \[planets] \[conditions]<br>
*__full__* - true or false (default), include or exclude planets that you haven't discovered yet. If set to true, will set the planet to surveyed.<br>
*__planets__* -  the __minimum__ number of planets a system must include (default 1)<br>
*__conditions__* - a comma-separated, __no-space__ list of conditions and times it needs to occur in the system: \[habitable=2,ore_rare=1] - **at least** two planets that are habitable and one that has transplutonic ore.<br>

    Example: Explore true 5 \[habitable=2]
    
2) SystemInRange \<entity> \<LY><br>
Searches for systems which are X LY away of an entity (cryosleeper, coronal tap, inactive gate).<br>

    Example: SystemInRange coronal_tap 10


<img src="./mod_pic.png">


#TODO:
- filter by stable locations/jump points
- filter by inactive gates
- filter by planet type
