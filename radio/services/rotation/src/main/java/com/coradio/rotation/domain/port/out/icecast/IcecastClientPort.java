package com.coradio.rotation.domain.port.out.icecast;

import com.coradio.rotation.domain.context.StationInfo;

public interface IcecastClientPort {

    StationInfo fetchStationInfo();
}
