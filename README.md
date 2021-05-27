# Explore_the_galaxy
A console script for Starsector<br><br>

This mod benefits mostly users that have Grand or Adjusted sector mods.<br>
Commands:<br>
1) Explore \[full] \[nr. of planets] \[conditions]<br>
*full* - true or false (default), include or exclude planets tha you haven't discovered yet. If set to true, will set the planet to surveyed.
*nr. of planets* -  the minimum number of planets a system must include (default 1)
*conditions* - a comma-separated, no-space list of conditions and times it needs to occure in the system: \[habitable=2,ore_rare=1] - **at least** two planets that are habitable and one that has transplutonic ore.
\t Example: Explore true 5 \[habitable=2]
2) SystemInRange \<entity> \<LY><br>
Searches for systems which are X LY away of an enitity (cryosleeper, coronal tap, inactive gate).<br>
\t Example: SystemInRange coronal_tap 10
