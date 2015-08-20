#include <sstream>

#include <jank/parse/expect/type.hpp>
#include <jank/translate/function/match_indirect.hpp>
#include <jank/translate/function/argument/resolve_type.hpp>
#include <jank/translate/expect/error/type/overload.hpp>

namespace jank
{
  namespace translate
  {
    namespace function
    {
      void match_indirect
      (
        cell::type_definition::type const &type,
        parse::cell::list const &list,
        std::shared_ptr<environment::scope> const &scope,
        std::function<void (cell::indirect_function_call)> callback
      )
      {
        /* TODO: Parse args, match args against type, throw on failure. */
        static_cast<void>(type);
        static_cast<void>(scope);
        static_cast<void>(callback);

        auto const arguments
        (function::argument::call::parse<cell::cell>(list, scope));

        /* TODO: Look up generics in type. */
        static_cast<void>(arguments);

        /* TODO: Match args size and types. */

        if(true) /* TODO: Final check. */
        {
          callback({});
          return;
        }

        std::stringstream ss;
        ss << "no matching function: "
           << parse::expect::type<parse::cell::type::ident>(list.data[0]).data
           << " with arguments: ";

        for(auto const &arg : arguments)
        {
          ss << arg.name << " : "
             << function::argument::resolve_type(arg.cell, scope).data.name
             << " ";
        }
        throw expect::error::type::overload
        { ss.str() };
      }
    }
  }
}