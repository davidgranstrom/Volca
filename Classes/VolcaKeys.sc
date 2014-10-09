// TODO: optimize "map" method when chaining multiple parameters..  

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
        var name = if(parameter.isArray) { parameter.join.asSymbol; } { parameter; };
        var func = {
            SendReply.kr(Impulse.kr(poll), ("/"++name).asSymbol, lfo.interpret.range(lo*127,hi*127));
        };
        OSCdef(name, {|msg| 
            var cc = msg[3];
            parameter.do {|param|
                midiOut.control(0, midiCCs[param], cc.round(1));
            };
        }, name);
        server.makeBundle(nil, {
            instances[name] !? { this.unmap(name) };
            instances.put(name, func.play);
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
