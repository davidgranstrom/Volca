VolcaKeys
=========

Notice
------

This class is still **under development** and might break.. but is already quite useful.

Description
-----------

Send control data (MIDI CC) to the *Korg Volca Keys* from SuperCollider.  

Examples
--------

Use the `map` method to control a parameter by passing in an LFO as a `String` or a `Function`.

    midiOut = ...
    k = VolcaKeys(midiOut);

    k.map('cutoff', "SinOsc.kr(1/3)");
    // or
    complexLFO = { 
        var x = SinOsc.kr(1/3) + LFDNoise1.kr(1/10); 
        x * 0.5; // scale range to +- 1
    };
    k.map('cutoff', complexLFO);

Use the `unmap` method to remove a LFO.

    k.unmap('cutoff');

The `clear` method removes all assigned controls.

    k.clear;
