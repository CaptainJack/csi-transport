package ru.capjack.csi.sandbox.server.netty

import ru.capjack.csi.core.server.ConnectionAuthorizer
import ru.capjack.tool.io.InputByteBuffer

class RushConnectionAuthorizer : ConnectionAuthorizer<Int> {
	override fun authorizeConnection(authorizationKey: InputByteBuffer): Int? {
		return if (authorizationKey.isReadable(4)) authorizationKey.readInt().takeIf { it > 0 }
		else null
	}
	
}