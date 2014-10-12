Volca
=====

Description
-----------

Send control data (MIDI CC) to the *Korg Volca* series of synthesizers from SuperCollider.  

Supported devices
-----------------

* Volca Keys  - OK
* Volca Bass  - N/A 
* Volca Beats - N/A

Examples
--------

Create a new `Volca` instance using the device name, passing in an instance of `MIDIOut`.

    ~midiOut = MIDIOut.newByName("a-MIDI-device", "MIDI-port");
    k = Volca(\keys, ~midiOut);

Use the `map` method to control a parameter by passing in a LFO as a `String` or a `Function`.

    k.map('cutoff', "SinOsc.kr(1/3)");
    // or
    ~complexLFO = { 
        var x = SinOsc.kr(1/3) + LFDNoise1.kr(1/10); 
        x * 0.5; // scale range to +- 1
    };
    k.map('cutoff', ~complexLFO);

Parameters can also be chained together by passing them as an array.

    k.map([ 'cutoff', 'lfo rate' ], "SinOsc.kr(1/10)");

Use the `unmap` method to remove a LFO.

    k.unmap('cutoff');

The `clear` method removes all assigned controls.

    k.clear;

TODO
----

Write native documentation.
