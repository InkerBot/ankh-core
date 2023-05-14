package org.inksnow.ankh.core.api.config;

import org.inksnow.ankh.core.api.ioc.IocLazy;
import org.inksnow.ankh.core.api.util.DcLazy;
import org.inksnow.ankh.core.api.util.IBuilder;

import javax.annotation.Nonnull;

public interface ConfigNameStrategy {
  @Nonnull
  String translateName(@Nonnull String name);

  static Factory factory(){
    return $internal$actions$.factory.get();
  }

  static Builder builder(){
    return factory().builder();
  }

  /**
   * Using this naming strategy will ensure that the bean name is unchanged.
   */
  static @Nonnull ConfigNameStrategy identity(){
    return factory().identity();
  }

  /**
   * Using this naming strategy will ensure that the first "letter" of the bean name is
   * capitalized when serialized to its config form.
   *
   * <p>Here's a few examples of the form "Java Bean Name" ---> "Config Bean Name":</p>
   * <ul>
   *   <li>someBeanName ---> SomeBeanName</li>
   *   <li>_someBeanName ---> _SomeBeanName</li>
   * </ul>
   */
  static @Nonnull ConfigNameStrategy upperCamelCase(){
    return factory().upperCamelCase();
  }

  /**
   * Using this naming strategy will ensure that the first "letter" of the bean name is
   * capitalized when serialized to its config form and the words will be
   * separated by a space.
   *
   * <p>Here's a few examples of the form "Java Bean Name" ---> "Config Field Name":</p>
   * <ul>
   *   <li>someBeanName ---> Some Bean Name</li>
   *   <li>_someBeanName ---> _Some Bean Name</li>
   * </ul>
   */
  static @Nonnull ConfigNameStrategy upperCamelCaseWithSpaces(){
    return factory().upperCamelCaseWithSpaces();
  }

  /**
   * Using this naming strategy will modify the bean name from its camel cased
   * form to a lower case field name where each word is separated by an underscore (_).
   *
   * <p>Here's a few examples of the form "Java Bean Name" ---> "Config Bean Name":</p>
   * <ul>
   *   <li>someBeanName ---> some_bean_name</li>
   *   <li>_someBeanName ---> _some_bean_name</li>
   *   <li>aStringBean ---> a_string_bean</li>
   *   <li>aURL ---> a_u_r_l</li>
   * </ul>
   */
  static @Nonnull ConfigNameStrategy lowerCaseWithUnderscores(){
    return factory().lowerCaseWithUnderscores();
  }

  /**
   * Using this naming policy with Gson will modify the bean name from its camel cased
   * form to a lower case field name where each word is separated by a dash (-).
   *
   * <p>Here's a few examples of the form "Java Field Name" ---> "JSON Field Name":</p>
   * <ul>
   *   <li>someBeanName ---> some-bean-name</li>
   *   <li>_someBeanName ---> _some-bean-name</li>
   *   <li>aStringBean ---> a-string-bean</li>
   *   <li>aURL ---> a-u-r-l</li>
   * </ul>
   */
  static @Nonnull ConfigNameStrategy lowerCaseWithDashes(){
    return factory().lowerCaseWithDashes();
  }

  interface Factory {
    @Nonnull Builder builder();

    /**
     * Using this naming strategy will ensure that the bean name is unchanged.
     */
    @Nonnull ConfigNameStrategy identity();

    /**
     * Using this naming strategy will ensure that the first "letter" of the bean name is
     * capitalized when serialized to its config form.
     *
     * <p>Here's a few examples of the form "Java Bean Name" ---> "Config Bean Name":</p>
     * <ul>
     *   <li>someBeanName ---> SomeBeanName</li>
     *   <li>_someBeanName ---> _SomeBeanName</li>
     * </ul>
     */
    @Nonnull ConfigNameStrategy upperCamelCase();

    /**
     * Using this naming strategy will ensure that the first "letter" of the bean name is
     * capitalized when serialized to its config form and the words will be
     * separated by a space.
     *
     * <p>Here's a few examples of the form "Java Bean Name" ---> "Config Field Name":</p>
     * <ul>
     *   <li>someBeanName ---> Some Bean Name</li>
     *   <li>_someBeanName ---> _Some Bean Name</li>
     * </ul>
     */
    @Nonnull ConfigNameStrategy upperCamelCaseWithSpaces();

    /**
     * Using this naming strategy will modify the bean name from its camel cased
     * form to a lower case field name where each word is separated by an underscore (_).
     *
     * <p>Here's a few examples of the form "Java Bean Name" ---> "Config Bean Name":</p>
     * <ul>
     *   <li>someBeanName ---> some_bean_name</li>
     *   <li>_someBeanName ---> _some_bean_name</li>
     *   <li>aStringBean ---> a_string_bean</li>
     *   <li>aURL ---> a_u_r_l</li>
     * </ul>
     */
    @Nonnull ConfigNameStrategy lowerCaseWithUnderscores();

    /**
     * Using this naming policy with Gson will modify the bean name from its camel cased
     * form to a lower case field name where each word is separated by a dash (-).
     *
     * <p>Here's a few examples of the form "Java Field Name" ---> "JSON Field Name":</p>
     * <ul>
     *   <li>someBeanName ---> some-bean-name</li>
     *   <li>_someBeanName ---> _some-bean-name</li>
     *   <li>aStringBean ---> a-string-bean</li>
     *   <li>aURL ---> a-u-r-l</li>
     * </ul>
     */
    @Nonnull ConfigNameStrategy lowerCaseWithDashes();
  }

  interface Builder extends IBuilder<Builder, ConfigNameStrategy> {
    /**
     * ensure that the first letter upper from source
     *
     * @return this
     */
    @Nonnull Builder upperCaseFirstLetter();

    /**
     * ensure all letter lower case
     *
     * @return this
     */
    @Nonnull Builder lowerCaseAllLetter();

    /**
     * ensure all letter upper case
     *
     * @return this
     */
    @Nonnull Builder upperCaseAllLetter();

    /**
     * the words will be separated by separator.
     *
     * @param separator the separator
     * @return this
     */
    @Nonnull Builder separateCamelCase(@Nonnull String separator);
  }

  class $internal$actions$ {
    private static DcLazy<Factory> factory = IocLazy.of(Factory.class);
  }
}
