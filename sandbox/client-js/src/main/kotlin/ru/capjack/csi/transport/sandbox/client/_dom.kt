package ru.capjack.csi.transport.sandbox.client

import org.w3c.dom.Document
import org.w3c.dom.Element

inline fun <E : Element?> Document.get(id: String): E {
	return getElementById(id).unsafeCast<E>()
}

inline fun <E : Element> Element.appendElement(name: String, init: E.() -> Unit): E {
	return appendElement<E>(name).apply(init)
}

inline fun <E : Element> Element.appendElement(name: String): E {
	return ownerDocument.unsafeCast<Document>().createElement(name).unsafeCast<E>().also {
		appendChild(it)
	}
}
