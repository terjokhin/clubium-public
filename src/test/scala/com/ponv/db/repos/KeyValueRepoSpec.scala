package com.ponv.db.repos

import com.ponv.db.PerSuiteDB

class KeyValueRepoSpec extends PerSuiteDB:

  test("Should return empty result if key is not exists") {
    val tx   = xa()
    val repo = KeyValueRepo(tx)
    val key  = "key_noop"

    repo.find(key).assertEquals(None)
  }

  test("Should save and retrieve key by value") {
    val tx    = xa()
    val repo  = KeyValueRepo(tx)
    val key   = "key1"
    val value = "some useful very useful data"

    val io = for {
      _     <- repo.put(key, value)
      found <- repo.find(key)
    } yield {
      found
    }

    io.assertEquals(Some(value))
  }

  test("Should delete key by value") {
    val tx    = xa()
    val repo  = KeyValueRepo(tx)
    val key   = "key2"
    val value = "some useful very useful data"

    val io = for {
      _     <- repo.put(key, value)
      _     <- repo.delete(key)
      found <- repo.find(key)
    } yield {
      found
    }

    io.assertEquals(None)
  }

  test("Should update key value") {
    val tx     = xa()
    val repo   = KeyValueRepo(tx)
    val key    = "key3"
    val value1 = "some useful very useful data1"
    val value2 = "some useful very useful data2"

    val io = for {
      _     <- repo.put(key, value1)
      _     <- repo.put(key, value2)
      found <- repo.find(key)
    } yield {
      found
    }

    io.assertEquals(Some(value2))
  }
