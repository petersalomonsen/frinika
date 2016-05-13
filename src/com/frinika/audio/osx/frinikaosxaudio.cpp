//
//  frinikaosxaudio.cpp
//  frinikaosxaudio
//
//  Created by Peter Salomonsen on 08.05.2016.
//  Copyright © 2016 Frinika. All rights reserved.
//

#include "frinikaosxaudio.hpp"
#include <iostream>
#include <CoreFoundation/CoreFoundation.h>
#import <AudioToolbox/AudioToolbox.h>

using namespace std;
typedef void (*FrinikaAudioCallback) (int,int,Float32 *,Float32 *);
FrinikaAudioCallback frinikaAudioCallback;

typedef struct MyAUGraphPlayer
{
    AudioStreamBasicDescription streamFormat;
    
    AUGraph graph;
    AUNode output;
    AUNode mixer;
    AUNode sine;
    AudioUnit audioUnits[3];
    
    
    AudioBufferList *inputBuffer;
    
    
    Float64 firstInputSampleTime;
    Float64 firstOutputSampleTime;
    Float64 inToOutSampleTimeOffset;
   
    
} MyAUGraphPlayer;

double currentPhase = 0;
OSStatus SineWaveRenderCallback(void * inRefCon,
                                AudioUnitRenderActionFlags * ioActionFlags,
                                const AudioTimeStamp * inTimeStamp,
                                UInt32 inBusNumber,
                                UInt32 inNumberFrames,
                                AudioBufferList * ioData)
{
    
    frinikaAudioCallback(inNumberFrames,inBusNumber,(Float32 * )ioData->mBuffers[0].mData,(Float32 * )ioData->mBuffers[1].mData);
    
    
    return noErr;
}


extern "C" void startAudioWithCallback(FrinikaAudioCallback func) {
    MyAUGraphPlayer *player = {0};
    MyAUGraphPlayer p = {0};
    player=&p;
    
    NewAUGraph(&player->graph);
    
    OSStatus result = 0;
    
    AudioStreamBasicDescription ASBD = {
        .mSampleRate       = 44100,
        .mFormatID         = kAudioFormatLinearPCM,
        .mFormatFlags      = kAudioFormatFlagsNativeFloatPacked,
        .mChannelsPerFrame = 2,
        .mFramesPerPacket  = 1,
        .mBitsPerChannel   = sizeof(Float32) * 8,
        .mBytesPerPacket   = sizeof(Float32),
        .mBytesPerFrame    = sizeof(Float32)
    };
    
    
    //Output
    {
        AudioComponentDescription description = {
            .componentType = kAudioUnitType_Output,
            .componentSubType = kAudioUnitSubType_DefaultOutput,
            .componentManufacturer = kAudioUnitManufacturer_Apple
        };
        result = AUGraphAddNode(player->graph, &description, &player->output);
        printf("err: %d\n", result);
        AudioComponent comp = AudioComponentFindNext(NULL, &description);
        result = AudioComponentInstanceNew(comp, &player->audioUnits[0]);
        printf("err: %d\n", result);
        result = AudioUnitInitialize(player->audioUnits[0]);
        printf("err: %d\n", result);
        
    }
    
    //Mixer
    {
        AudioComponentDescription description = {
            .componentType = kAudioUnitType_Mixer,
            .componentSubType = kAudioUnitSubType_StereoMixer,
            .componentManufacturer = kAudioUnitManufacturer_Apple
        };
        result = AUGraphAddNode(player->graph, &description, &player->mixer);
        printf("err: %d\n", result);
        AudioComponent comp = AudioComponentFindNext(NULL, &description);
        result = AudioComponentInstanceNew(comp, &player->audioUnits[1]);
        printf("err: %d\n", result);
        
        
    }
    
    
    //Sine
    {
        AudioComponentDescription description = {
            .componentType = kAudioUnitType_Generator,
            .componentSubType = kAudioUnitSubType_ScheduledSoundPlayer,
            .componentManufacturer = kAudioUnitManufacturer_Apple
        };
        result = AUGraphAddNode(player->graph, &description, &player->sine);
        printf("err: %d\n", result);
        AudioComponent comp = AudioComponentFindNext(NULL, &description);
        result = AudioComponentInstanceNew(comp, &player->audioUnits[2]);
        printf("err: %d\n", result);
        result = AudioUnitInitialize(player->audioUnits[2]);
        printf("err: %d\n", result);
        
    }
    
    
    
    
    result = AUGraphConnectNodeInput(player->graph,
                                     player->sine,
                                     0,
                                     player->mixer,
                                     0);
    printf("err: %d\n", result);
    
    result = AUGraphConnectNodeInput(player->graph,
                                     player->mixer,
                                     0,
                                     player->output,
                                     0);
    printf("err: %d\n", result);
    
    result = AUGraphOpen(player->graph);
    printf("err: %d\n", result);
    
    
    UInt32 numbuses = 1;
    
    
    result = AudioUnitSetProperty(player->audioUnits[1], kAudioUnitProperty_ElementCount, kAudioUnitScope_Input, 0, &numbuses, sizeof(numbuses));
    printf("err: %d\n", result);
    
    frinikaAudioCallback = func;
    
    for (UInt32 i = 0; i <= numbuses; ++i) {
        // setup render callback struct
        AURenderCallbackStruct rcbs;
        rcbs.inputProc = &SineWaveRenderCallback;
        rcbs.inputProcRefCon = &player;
        
        printf("set AUGraphSetNodeInputCallback\n");
        
        // set a callback for the specified node's specified input
        result = AUGraphSetNodeInputCallback(player->graph, player->mixer, i, &rcbs);
        printf("AUGraphSetNodeInputCallback err: %d\n", result);
        
        printf("set input bus %d, client kAudioUnitProperty_StreamFormat\n", (unsigned int)i);
        
        // set the input stream format, this is the format of the audio for mixer input
        result = AudioUnitSetProperty(player->audioUnits[1], kAudioUnitProperty_StreamFormat, kAudioUnitScope_Input, i, &ASBD, sizeof(ASBD));
        printf("err: %d\n", result);
    }
    
    
    
    result = AudioUnitSetProperty(player->audioUnits[1], kAudioUnitProperty_StreamFormat, kAudioUnitScope_Output, 0, &ASBD, sizeof(ASBD));
    printf("err: %d\n", result);
    
    
    OSStatus status = AUGraphInitialize(player->graph);
    printf("err: %d\n", status);
    
    
    player->firstOutputSampleTime = -1;
    AudioOutputUnitStart(player->audioUnits[0]);
    AUGraphStart(player->graph);

}