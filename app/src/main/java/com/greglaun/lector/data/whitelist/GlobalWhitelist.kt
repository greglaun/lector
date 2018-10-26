package com.greglaun.lector.data.whitelist

val EXPECTED_SIZE = 10000 // Just a random guess for now. Performance testing needed.
val FALSE_POSITIVE_RATE = 80 // Just a random guess for now. Performance testing needed.

// A threadsafe singleton whitelist
//object GlobalWhitelist : ProbabilisticSet<String> {
//    val countingBloomFilter  = CountingBloomFilterMemory<String>(
//            FilterBuilder(EXPECTED_SIZE, FALSE_POSITIVE_RATE))
//
//    override fun probablyContains(element: String): Boolean {
//        return countingBloomFilter.contains(element)
//    }
//
//    override fun add(element: String) {
//        countingBloomFilter.add(element) // Ignore return value for now
//    }
//
//    override fun delete(element: String) {
//        countingBloomFilter.remove(element) // Ignore return value for now
//    }
//
//}