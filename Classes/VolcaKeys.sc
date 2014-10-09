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

    map {|parameter, lfo, lo=0, hi=1|
        server.makeBundle(nil, {
            parameter.do {|param|
                var func = {
                    var val = if(lfo.isFunction) { SynthDef.wrap(lfo) } { lfo.interpret };
                    val = val.range(lo*127,hi*127);
                    SendReply.kr(Impulse.kr(poll), ("/"++param).asSymbol, val);
                };

                instances[param] !? { this.unmap(param) };

                OSCdef(param, {|msg| 
                    var cc = msg[3];
                    midiOut.control(0, midiCCs[param], cc.round(1));
                }, param);
                instances.put(param, func.play);
            };
        });
    }

    unmap {|parameter|
        server.makeBundle(nil, {
            parameter.do {|param|
                OSCdef(param).free;
                instances[param].free;
            };
        });
    }

    clear {
        server.makeBundle(nil, {
            instances.keysValuesDo {|param, synth| 
                OSCdef(param).free;
                synth.free; 
            };
        });
    }

    getCC {|parameter|
        ^midiCCs[parameter] ?? { "Unkown parameter: %\n".postf(parameter) };
    }
}
