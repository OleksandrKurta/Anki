//
//
//    @DisplayName("PATCH /api/v1/cards")
//    @Test
//    fun `should update an existing card`() {
//        // given
//        val cardToUpdate: CardDtoResponse = generateRandomCard()
//        val currentModel = dataSource.insert(mapDtoToRepository(newCard))
//        cleanupModels.add(currentModel)
//        cardToUpdate.id = currentModel.id.toString()
//
//        // when
//        val performPatchRequest = mockMvc.patch(baseUrl) {
//            contentType = MediaType.APPLICATION_JSON
//            content = objectMapper.writeValueAsString(cardToUpdate)
//        }
//        val resultCard = performPatchRequest.andReturn()
//            .response
//            .contentAsString
//            .let { objectMapper.readValue(it, CardDtoResponse::class.java) }
//        cleanupModels.add(mapDtoToRepository(resultCard))
//
//        // then
//        performPatchRequest
//            .andDo { print() }
//            .andExpect {
//                status { isOk() }
//                content {
//                    contentType(MediaType.APPLICATION_JSON)
//                    json(objectMapper.writeValueAsString(cardToUpdate))
//                }
//            }
//    }
//
//    @DisplayName("PATCH /api/v1/cards non existent card")
//    @Test
//    fun `should get BAD REQUEST when card not exists`() {
//        // given
//        val invalidIdCard = generateRandomCard()
//        invalidIdCard.id = ObjectId().toString()
//
//        // when
//        val performPatchRequest = mockMvc.patch(baseUrl) {
//            contentType = MediaType.APPLICATION_JSON
//            content = objectMapper.writeValueAsString(invalidIdCard)
//        }
//
//        // then
//        performPatchRequest
//            .andDo { print() }
//            .andExpect { status { isNotFound() } }
//    }
//
//
//    @DisplayName("DELETE api/v1/cards/{id}")
//    @Test
//    @DirtiesContext
//    fun `should delete the card`() {
//        // given
//        val model = dataSource.insert(mapDtoToRepository( newCard))
//        val performDelete = mockMvc.delete("$baseUrl/${model.id.toString()}")
//        // when, then
//            performDelete.andDo { print() }
//            .andExpect {
//                status { isOk() }
//                content { (objectMapper.writeValueAsString(model.id.toString())) }
//            }
//    }
//
//    @DisplayName("DELETE api/v1/cards/{id}")
//    @Test
//    fun `should get NOT FOUND when no card exists`() {
//        // given
//        newCard?.id = ObjectId().toString()
//
//        // when/then
//        mockMvc.delete("$baseUrl/$newCard.id")
//            .andDo { print() }
//            .andExpect { status { isNotFound() } }
//    }
//
//}
