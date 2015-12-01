# Munchcal-Reagent

The website for [MunchCal](https://munchcal.com). Build with [Reagent](http://reagent-project.github.io/), and ClojureScript.

## Deploying
There isn't a complicated build pipeline setup. It is a pretty standard Reagent project.

Run this:

    lein with-profile uberjar uberjar
    scp target/munchcal-reagent.jar [destination]

It defaults to port 3000.
