module.exports = {
  content: ["./*.js"],
  plugins: [
    require('daisyui'),
    require('tailwind-scrollbar'),
  ],
  daisyui: {
    logs: false, // otherwise daisy logs its ui version
    themes: [
//      "lofi"
//      'cupcake',
//      'autumn',
        "fantasy",
        {
          dracula: {
            ...require("daisyui/src/colors/themes")["[data-theme=dracula]"],
            "base-100": "#2A303C",
            "base-200": "#242933",
            "base-300": "#20252E",
          },
        },
    ]
  },
};