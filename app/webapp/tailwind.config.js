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
        'dark'
    ]
  },
};