// ===========================================================================
// Title         : Volca
// Description   : Map LFO's to the Korg Volca series synthesizers
// Copyright (c) : David Granstrom 2014
// ===========================================================================

Volca {

    var midiOut, channel, pollTime, server;
    var <controls, instances;

    *new {|device, aMidiOut, channel=0, pollTime=30, server|
        ^super.newCopyArgs(aMidiOut, channel, pollTime, server ? Server.default).init(device);
    }

    init {|device|
        if(server.serverRunning.not) { "Server must be booted".throw };
        instances = ();
        controls  = switch(device)
        { 'keys' } {
            (
                'portamento'       : 5,
                'expression'       : 11,
                'voice'            : 40,
                'octave'           : 41,
                'detune'           : 42,
                'vco eg int'       : 43,
                'cutoff'           : 44,
                'vcf eg int'       : 45,
                'lfo rate'         : 46,
                'lfo pitch int'    : 47,
                'lfo cutoff int'   : 48,
                'eg attack'        : 49,
                'eg decay/release' : 50,
                'eg sustain'       : 51,
                'delay time'       : 52,
                'delay feedback'   : 53,
            )
        }
        { 'bass' } {
            (
                'slide time'       : 5,
                'expression'       : 11,
                'octave'           : 40,
                'lfo rate'         : 41,
                'lfo int'          : 42,
                'vco pitch 1'      : 43,
                'vco pitch 2'      : 44,
                'vco pitch 3'      : 45,
                'eg attack'        : 46,
                'eg decay/release' : 47,
                'cutoff eg int'    : 48,
                'gate time'        : 49,
            )
        }
        { 'beats' } {
            (
                'kick'             : 40,
                'snare'            : 41,
                'lo tom'           : 42,
                'hi tom'           : 43,
                'cl hat'           : 44,
                'op hat'           : 45,
                'clap'             : 46,
                'claves'           : 47,
                'agogo'            : 48,
                'crash'            : 49,
                'clap PCM'         : 50,
                'calves PCM'       : 51,
                'agogo PCM'        : 52,
                'crash PCM'        : 53,
                'stutter time'     : 54,
                'stutter depth'    : 55,
                'tom decay'        : 56,
                'closed hat decay' : 57,
                'open hat decay'   : 58,
                'hat grain'        : 59,
            )
        }
        { "Device not supported!".throw };
    }

    map {|parameter, lfo, lo=0, hi=1|
        var mapFunc = {
            parameter.do {|param|
                var func = {
                    var val = if(lfo.isFunction) { SynthDef.wrap(lfo) } { lfo.interpret };
                    val = val.range(lo*127,hi*127);
                    SendReply.kr(Impulse.kr(pollTime), ("/"++param).asSymbol, val);
                };

                instances[param] !? { this.unmap(param) };

                OSCdef(param, {|msg|
                    var cc = msg[3];
                    midiOut.control(channel, controls[param], cc.round(1));
                }, param);

                instances.put(param, func.play);
            };
        };
        if(this.validate(parameter)) {
            server.makeBundle(nil, mapFunc);
        } {
            this.showError(parameter);
        }
    }

    unmap {|parameter|
        var unmapFunc = {
            parameter.do {|param|
                OSCdef(param).free;
                instances[param].free;
                instances[param] = nil;
            };
        };
        if(this.validate(parameter)) {
            server.makeBundle(nil, unmapFunc);
        } {
            this.showError(parameter);
        }
    }

    clear {
        server.makeBundle(nil, {
            instances.keysValuesDo {|param, synth|
                OSCdef(param).free;
                synth.free;
            };
            instances = ();
        });
    }

    validate {|parameter|
        ^controls.atAll([ parameter ].flat).select {|x| x.isNil }.isEmpty;
    }

    showError {|parameter|
        if(parameter.isArray.not) {
            "'%' is not a valid parameter!".format(parameter).warn;
        } {
            "Parameter array % contains an invalid parameter!".format(parameter).warn;
        }
    }
}

// TODO:
//
// * optimize "map" method to only use one synth/OSCdef when chaining multiple
// parameters to the same LFO. We still want to have granular control for each
// parameter i.e. we want to be able to unmap a single parameter even though
// they are chained.
