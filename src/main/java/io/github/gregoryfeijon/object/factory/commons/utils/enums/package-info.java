/**
 * Enum utility classes for the Object Factory Commons library.
 * <p>
 * This package provides utilities for working with Java enums, enabling
 * flexible enum constant lookup by property values or names.
 * <p>
 * <strong>Available Utilities:</strong>
 * <ul>
 *   <li>{@link io.github.gregoryfeijon.object.factory.commons.utils.enums.EnumUtil EnumUtil} -
 *       Enum lookup utilities providing:
 *       <ul>
 *         <li>Finding enum constants by property value using getter methods</li>
 *         <li>Finding enum constants by name (case-sensitive and case-insensitive)</li>
 *         <li>Both Optional-returning and null-returning variants</li>
 *       </ul>
 *   </li>
 * </ul>
 * <p>
 * <strong>Usage Examples:</strong>
 * <pre>{@code
 * // Find enum by property value
 * Optional<Status> status = EnumUtil.getEnum(Status.class, Status::getCode, "A");
 *
 * // Find enum by name (case-sensitive)
 * Optional<Status> active = EnumUtil.getEnumByName(Status.class, "ACTIVE");
 *
 * // Find enum by name (case-insensitive)
 * Optional<Status> found = EnumUtil.getEnumByName(Status.class, "active", true);
 *
 * // Null-returning variant
 * Status result = EnumUtil.getEnumOrNull(Status.class, Status::getId, 1);
 * }</pre>
 * <p>
 * <strong>Benefits over Enum.valueOf():</strong>
 * <ul>
 *   <li>No exception thrown when value not found</li>
 *   <li>Supports property-based lookup, not just name</li>
 *   <li>Case-insensitive name matching option</li>
 *   <li>Null-safe operation</li>
 * </ul>
 *
 * @author Gregory Maximiano Feijon
 * @since 1.0
 * @see io.github.gregoryfeijon.object.factory.commons.utils.enums.EnumUtil
 */
package io.github.gregoryfeijon.object.factory.commons.utils.enums;
