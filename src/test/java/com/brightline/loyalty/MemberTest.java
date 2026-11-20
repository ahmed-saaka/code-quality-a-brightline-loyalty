package com.brightline.loyalty;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
@ExtendWith (MockitoExtension.class)
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

 @Mock
    MemberRepository mockRepository;
    @InjectMocks 
    PointsCalculator calculator;
    @Test
   
    void shouldRejectNullMembershipTier() {
        // Set up the mock repository to return a specific member
        Member mockMember = new Member("m5", "KWame",  null);
        
        when(mockRepository.findById("m5")).thenReturn(mockMember);

        // Call the method under test
                assertThrows(IllegalArgumentException.class,
                        () -> calculator.calculatePoints("m5", new BigDecimal("100.00")));

                verify(mockRepository, times(1)).findById("m5");
        
       
    }


    @Test
public void shouldRejectUnsupportedMembershipTier() {

    MemberRepository repository = mock(MemberRepository.class);
    PointsCalculator calculator = new PointsCalculator(repository);

    Member member = new Member("m4", "Kwame", "PLATINUM");

    when(repository.findById("m4")).thenReturn(member);

    assertThrows(
            IllegalArgumentException.class,
            () -> calculator.calculatePoints(
                    "m4",
                    new BigDecimal("100")
            )
    );

    verify(repository, times(1)).findById("m4");
}


@Test
public void shouldRejectUnknownMemberId() {

    // Arrange
    MemberRepository repository = mock(MemberRepository.class);
    PointsCalculator calculator = new PointsCalculator(repository);

    // Act & Assert
    assertThrows(
            IllegalArgumentException.class,
            () -> calculator.calculatePoints(
                    "m999",
                    BigDecimal.valueOf(100)
            )
    );

    // Verify that the repository was checked exactly once
    verify(repository, times(1)).findById("m999");
}




@Test
void shouldRejectEmptyMembershipTier(){
    Member member = new Member("m6", "Akua", "");
    when(mockRepository.findById("m6")).thenReturn(member);
    assertThrows(IllegalArgumentException.class, () ->
     calculator.calculatePoints("m6", new BigDecimal("100")));
     verify(mockRepository, times(1)).findById("m6");

}

@Test
void shouldRejectWhitespaceMembershipTier(){
    Member member = new Member("m6", "Akua", " ");
    when(mockRepository.findById("m6")).thenReturn(member);
    assertThrows(IllegalArgumentException.class, () ->
     calculator.calculatePoints("m6", new BigDecimal("100")));
     verify(mockRepository, times(1)).findById("m6");

}





}
