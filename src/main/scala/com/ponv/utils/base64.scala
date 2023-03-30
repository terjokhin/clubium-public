package com.ponv.utils

import java.util.Base64

def fromBase64(str: String): String = new String(Base64.getDecoder.decode(str.getBytes))
