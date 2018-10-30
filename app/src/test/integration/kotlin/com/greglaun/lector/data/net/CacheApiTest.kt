package com.greglaun.lector.data.net


///**
// * Test of the CacheApi interface
// *
// */
//class CacheApiTest {
//    val dogUrlString = "https://en.wikipedia.org/robots.txt"
//
//    // todo(security): https
//    // todo(efficiency): efficient string operations
//    val http = "http:"
//    fun cleanImageUrls(urlString : String) : Optional<String> {
//        when {
//            urlString.startsWith("//upload") -> return Optional.of(http + urlString)
//            urlString.startsWith("/static/images") -> return Optional.of("$http//wikipedia.org$urlString")
//            urlString.contains("Special:CentralAutoLogin") -> return Optional.empty()
//            else -> return Optional.of(urlString)
//        }
//    }
//
//    fun saveFile(response: Response, filename : String) {
//        try {
//            val file = File("testfiles/" + filename)
//            file.writeBytes(response.bodyAsBytes())
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//    }
//
//    private fun makeFilesLocal(elements: Elements) {
//        val imageFolder = "./"
//        for (element in elements) {
//            if (element.attr("src").startsWith("/static/images/")) {
//                element.attr("src",
//                        element.attr("src")
//                                .replace("/static/images/", imageFolder))
//            } else if (element.attr("src").startsWith("//upload.wikimedia.org")) {
//                element.attr("src", imageFolder +
//                        element.attr("src").substringAfterLast('/')
//                                )
//                }
//        }
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun fetch_text_only() {
//        val uri = URI(dogUrlString)
//
//        val client = OkHttpClient()
//        val wikiFetcher = JsoupWikiFetcher(client)
//        wikiFetcher.fetchTextOnly(uri).subscribe{ result ->
//            val spiderDotText = result.fold({""}, {it})
//            assertTrue(spiderDotText.contains("grub-client"))
//            assertTrue(spiderDotText.contains("https://bugzilla.wikimedia.org/show_bug.cgi?id=14075"))
//        }
//    }
//
//    @Test
//    fun code_here() {
//        val doc = Jsoup.connect("https://en.wikipedia.org/wiki/Dog").get()
//        val imageElements = doc.getElementsByTag("img")
////        // todo(con[currency, measurement): Get empirical evidence vs downloading on one thread
////        Flowable.fromIterable(imageElements)
////                .parallel()
////                .runOn(Schedulers.io())
////                .map {
////                    it -> cleanImageUrls(it.attr("src"))
////                }
////                .filter{it -> it.isPresent}
////                .map {
////                    it ->
////                    val result = Jsoup.connect(it.get())
////                            .ignoreContentType(true).execute()
////                    saveFile(result, result.url().toString().substringAfterLast('/'))
////                    println("Wrote " + result.url().toString().substringAfterLast('/'))
////                }
////                .sequential()
////                .blockingLast()
//        makeFilesLocal(imageElements) // Warning: Side effects
//        File("testfiles/Dog.html").printWriter().use { out ->
//            out.println(doc)
//        }
//    }
//
//}
//
////upload.wikimedia.org/wikipedia/commons/thumb/f/fc/Padlock-silver.svg/20px-Padlock-silver.svg.png
