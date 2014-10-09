// TODO: 
//
// * optimize "map" method when chaining multiple parameters..  
// but how can we know if a parameter has already been mapped?
//
// * Supply a Function instead of a String as LFO in map method. Use SynthDef.wrap.
//

VolcaKeys {

    var midiOut, poll, server;
    var midiCCs, instances;

    *new {|aMidiOut, pollTime=30, server|
        ^super.newCopyArgs(aMidiOut, pollTime, server ? Server.default).init;
    }

    init {
        instances = ();
        midiCCs   = (
            'portamento'       :  5,
            'expression'       :  11,
            'voice'            :  40,
            'octave'           :  41,
            'detune'           :  42,
            'vco_eg_int'       :  43,
            'cutoff'           :  44,
            'vcf_eg_int'       :  45,
            'lfo_rate'         :  46,
            'lfo_pitch_int'    :  47,
            'lfo_cutoff_int'   :  48,
            'eg_attack'        :  49,
            'eg_decay/release' :  50,
            'eg_sustain'       :  51,
            'delay_time'       :  52,
            'delay_feedback'   :  53,
        );
    }

    map {|param, lfo, lo=0, hi=1|
        server.makeBundle(nil, {
            param.do {|p|
                var x = {
                    SendReply.kr(Impulse.kr(poll), ("/"++p).asSymbol, lfo.interpret.range(lo*127,hi*127));
                };
                OSCdef(p, {|msg| 
                    var cc = msg[3];
                    midiOut.control(0, midiCCs[p], cc.round(1));
                }, p);
                instances.put(p, x.play);
            };
        });
    }

    unmap {|param|
        server.makeBundle(nil, {
            param.do {|p|
                OSCdef(p).free;
                instances[p].free;
            };
        });
    }

    clear {
        server.makeBundle(nil, {
            instances.keysValuesDo {|key, syn| 
                OSCdef(key).free;
                syn.free 
            };
        });
    }

    getCC {|param|
        ^midiCCs[param] ?? { "Unkown parameter: %\n".postf(param) };
    }
}
