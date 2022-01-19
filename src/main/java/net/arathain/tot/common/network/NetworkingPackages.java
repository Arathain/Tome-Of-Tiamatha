package net.arathain.tot.common.network;

import net.arathain.tot.common.util.StringUtils;
import net.minecraft.util.Identifier;

public class NetworkingPackages {

        // Id for sending chain connection updates over the network.
        public static final Identifier S2C_STRING_ATTACH_PACKET_ID = StringUtils.identifier("s2c_string_attach_packet_id");
        public static final Identifier S2C_STRING_DETACH_PACKET_ID = StringUtils.identifier("s2c_string_detach_packet_id");
        public static final Identifier S2C_MULTI_STRING_ATTACH_PACKET_ID = StringUtils.identifier("s2c_multi_string_attach_packet_id");
        public static final Identifier S2C_SPAWN_STRING_COLLISION_PACKET = StringUtils.identifier("s2c_spawn_string_collision_packet_id");
        public static final Identifier S2C_SPAWN_STRING_KNOT_PACKET = StringUtils.identifier("s2c_spawn_string_knot_packet_id");

}
