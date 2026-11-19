package com.brightline.loyalty;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MemberTest {
    @Test 
    void testExceptionIsThrown() {
        assertThrows(RuntimeException.class, () -> {
            throw new RuntimeException();
        });
    }

    @Test 
    void testInValidId(){
        assertThrows(IllegalArgumentException.class, ()-> {
            new Member(null, "med", "GOLD");
           
        });
    }


        @Test 
    void testEmptyId(){
        assertThrows(IllegalArgumentException.class, ()-> {
            new Member("", "med", "GOLD");
           
        });
    }

           @Test 
    void testwhiteSpaced(){
        assertThrows(IllegalArgumentException.class, ()-> {
            new Member("   ", "med", "GOLD");
           
        });
    }




}
