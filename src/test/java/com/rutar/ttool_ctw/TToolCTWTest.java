package com.rutar.ttool_ctw;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

// ............................................................................
/// Базове тестування програми
/// @author Rutar_Andriy
/// 14.01.2026

@DisplayName("TToolCTWTest class")
public class TToolCTWTest {

// ============================================================================

@Test
@DisplayName("Should pass")
void shouldAnswerWithTrue()
  { assertTrue(true); }

// ============================================================================

@Test
@DisplayName("File .empty exist")
void fileEmptyExist()
  { assertNotNull(getClass().getResource(".empty")); }

// ============================================================================
    
// @Test
// @Disabled("skipped")
// @DisplayName("Should skip")
// void shouldSkip()
//   { fail("This error will be skipped"); }

// ============================================================================

// @Test
// @DisplayName("Should fail")
// void shouldFail()
//   { fail("Some error ..."); }

// Кінець класу TToolCTWTest ==================================================

}
