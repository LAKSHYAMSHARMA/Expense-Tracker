export const getErrorMessage = (error, fallback = 'Something went wrong. Please try again.') => {
  const message = error?.response?.data?.message || error?.response?.data?.error || error?.message;
  return message || fallback;
};
