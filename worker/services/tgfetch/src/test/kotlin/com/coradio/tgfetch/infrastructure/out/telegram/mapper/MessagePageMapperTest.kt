package com.coradio.tgfetch.infrastructure.out.telegram.mapper

import com.coradio.tgfetch.infrastructure.out.telegram.mapper.MockEntities.mockMessagePageResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MessagePageMapperTest {

    @Test
    fun `toDomain should return valid domain object`() {
        val result = MessagePageMapper.toDomain(mockMessagePageResponse)

        assertEquals(mockMessagePageResponse.items.count(), result.items.count())
        assertEquals(mockMessagePageResponse.nextCursor, result.nextCursor)
        assertEquals(mockMessagePageResponse.hasMore, result.hasMore)
    }

}
